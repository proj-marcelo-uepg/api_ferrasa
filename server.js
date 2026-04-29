const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const app = express();
app.use(express.json());

// --- Banco de dados SQLite ---
const db = new sqlite3.Database(path.join(__dirname, 'database.db'));

// Criação das tabelas (conforme modelo)
db.serialize(() => {
  db.run(`
    CREATE TABLE IF NOT EXISTS jogo (
      cod_jogador INTEGER PRIMARY KEY AUTOINCREMENT,
      nome VARCHAR(60) NOT NULL,
      email VARCHAR(60) NOT NULL UNIQUE,
      datanasc DATE NOT NULL
    )
  `);
  db.run(`
    CREATE TABLE IF NOT EXISTS pagamento (
      cod_pagamento INTEGER PRIMARY KEY AUTOINCREMENT,
      ano SMALLINT NOT NULL,
      mes TINYINT NOT NULL,
      valor NUMERIC(10,2) NOT NULL,
      cod_jogador INTEGER NOT NULL,
      FOREIGN KEY (cod_jogador) REFERENCES jogo(cod_jogador) ON DELETE CASCADE
    )
  `);
});

// --- Função helpers ---
const runQuery = (sql, params = []) => {
  return new Promise((resolve, reject) => {
    db.run(sql, params, function(err) {
      if (err) reject(err);
      else resolve(this);
    });
  });
};

const getQuery = (sql, params = []) => {
  return new Promise((resolve, reject) => {
    db.get(sql, params, (err, row) => {
      if (err) reject(err);
      else resolve(row);
    });
  });
};

const allQuery = (sql, params = []) => {
  return new Promise((resolve, reject) => {
    db.all(sql, params, (err, rows) => {
      if (err) reject(err);
      else resolve(rows);
    });
  });
};

// --- Rotas para Jogadores ---

// Criar jogador
app.post('/api/jogadores', async (req, res) => {
  try {
    const { nome, email, datanasc } = req.body;
    if (!nome || !email || !datanasc) {
      return res.status(400).json({ erro: 'Nome, email e datanasc são obrigatórios' });
    }
    // verifica email duplicado
    const existente = await getQuery('SELECT 1 FROM jogo WHERE email = ?', [email]);
    if (existente) {
      return res.status(409).json({ erro: 'Email já cadastrado' });
    }
    const result = await runQuery(
      'INSERT INTO jogo (nome, email, datanasc) VALUES (?, ?, ?)',
      [nome, email, datanasc]
    );
    const novo = await getQuery('SELECT * FROM jogo WHERE cod_jogador = ?', [result.lastID]);
    res.status(201).json(novo);
  } catch (err) {
    res.status(500).json({ erro: err.message });
  }
});

// Listar todos os jogadores
app.get('/api/jogadores', async (req, res) => {
  try {
    const jogadores = await allQuery('SELECT * FROM jogo ORDER BY cod_jogador');
    res.json(jogadores);
  } catch (err) {
    res.status(500).json({ erro: err.message });
  }
});

// Buscar jogador por ID
app.get('/api/jogadores/:id', async (req, res) => {
  try {
    const jogador = await getQuery('SELECT * FROM jogo WHERE cod_jogador = ?', [req.params.id]);
    if (!jogador) return res.status(404).json({ erro: 'Jogador não encontrado' });
    res.json(jogador);
  } catch (err) {
    res.status(500).json({ erro: err.message });
  }
});

// Atualizar jogador
app.put('/api/jogadores/:id', async (req, res) => {
  try {
    const { nome, email, datanasc } = req.body;
    const id = req.params.id;
    const jogador = await getQuery('SELECT * FROM jogo WHERE cod_jogador = ?', [id]);
    if (!jogador) return res.status(404).json({ erro: 'Jogador não encontrado' });

    if (email && email !== jogador.email) {
      const existente = await getQuery('SELECT 1 FROM jogo WHERE email = ? AND cod_jogador != ?', [email, id]);
      if (existente) return res.status(409).json({ erro: 'Email já cadastrado para outro jogador' });
    }

    await runQuery(
      'UPDATE jogo SET nome = ?, email = ?, datanasc = ? WHERE cod_jogador = ?',
      [nome || jogador.nome, email || jogador.email, datanasc || jogador.datanasc, id]
    );
    const atualizado = await getQuery('SELECT * FROM jogo WHERE cod_jogador = ?', [id]);
    res.json(atualizado);
  } catch (err) {
    res.status(500).json({ erro: err.message });
  }
});

// Deletar jogador (cascade delete nos pagamentos)
app.delete('/api/jogadores/:id', async (req, res) => {
  try {
    const result = await runQuery('DELETE FROM jogo WHERE cod_jogador = ?', [req.params.id]);
    if (result.changes === 0) return res.status(404).json({ erro: 'Jogador não encontrado' });
    res.status(204).send();
  } catch (err) {
    res.status(500).json({ erro: err.message });
  }
});

// --- Rotas para Pagamentos ---

// Criar pagamento
app.post('/api/pagamentos', async (req, res) => {
  try {
    const { ano, mes, valor, cod_jogador } = req.body;
    if (!ano || !mes || !valor || !cod_jogador) {
      return res.status(400).json({ erro: 'ano, mes, valor e cod_jogador são obrigatórios' });
    }
    // verifica se jogador existe
    const jogador = await getQuery('SELECT * FROM jogo WHERE cod_jogador = ?', [cod_jogador]);
    if (!jogador) return res.status(404).json({ erro: 'Jogador não encontrado' });

    // verifica duplicidade (mesmo jogador, ano e mês)
    const duplicado = await getQuery(
      'SELECT 1 FROM pagamento WHERE cod_jogador = ? AND ano = ? AND mes = ?',
      [cod_jogador, ano, mes]
    );
    if (duplicado) return res.status(409).json({ erro: 'Pagamento já existe para este jogador neste mês/ano' });

    const result = await runQuery(
      'INSERT INTO pagamento (ano, mes, valor, cod_jogador) VALUES (?, ?, ?, ?)',
      [ano, mes, valor, cod_jogador]
    );
    const novoPag = await getQuery('SELECT * FROM pagamento WHERE cod_pagamento = ?', [result.lastID]);
    res.status(201).json({
      ...novoPag,
      nome_jogador: jogador.nome
    });
  } catch (err) {
    res.status(500).json({ erro: err.message });
  }
});

// Listar pagamentos (com filtro opcional ?jogadorId=xx)
app.get('/api/pagamentos', async (req, res) => {
  try {
    const { jogadorId } = req.query;
    let sql = `
      SELECT p.*, j.nome as nome_jogador 
      FROM pagamento p 
      JOIN jogo j ON p.cod_jogador = j.cod_jogador
    `;
    let params = [];
    if (jogadorId) {
      sql += ' WHERE p.cod_jogador = ?';
      params.push(jogadorId);
    }
    sql += ' ORDER BY p.ano DESC, p.mes DESC';
    const rows = await allQuery(sql, params);
    res.json(rows);
  } catch (err) {
    res.status(500).json({ erro: err.message });
  }
});

// Buscar pagamento por ID
app.get('/api/pagamentos/:id', async (req, res) => {
  try {
    const pag = await getQuery(`
      SELECT p.*, j.nome as nome_jogador 
      FROM pagamento p 
      JOIN jogo j ON p.cod_jogador = j.cod_jogador 
      WHERE p.cod_pagamento = ?
    `, [req.params.id]);
    if (!pag) return res.status(404).json({ erro: 'Pagamento não encontrado' });
    res.json(pag);
  } catch (err) {
    res.status(500).json({ erro: err.message });
  }
});

// Deletar pagamento
app.delete('/api/pagamentos/:id', async (req, res) => {
  try {
    const result = await runQuery('DELETE FROM pagamento WHERE cod_pagamento = ?', [req.params.id]);
    if (result.changes === 0) return res.status(404).json({ erro: 'Pagamento não encontrado' });
    res.status(204).send();
  } catch (err) {
    res.status(500).json({ erro: err.message });
  }
});

// Iniciar servidor
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`API rodando em http://localhost:${PORT}`);
});