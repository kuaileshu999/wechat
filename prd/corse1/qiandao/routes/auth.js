const express = require('express');
const router = express.Router();
const jwt = require('jsonwebtoken');
const { User } = require('../models');

const SECRET_KEY = 'your-secret-key';

router.post('/login', async (req, res) => {
  const { username, password } = req.body;

  if (!username || !password) {
    return res.status(400).json({ message: 'Username and password are required' });
  }

  try {
    let user = await User.findOne({ where: { username } });

    if (!user) {
      // Auto register
      user = await User.create({ username, password });
      console.log(`User ${username} registered automatically.`);
    } else {
      // Check password (simple comparison for demo, use bcrypt in production)
      if (user.password !== password) {
        return res.status(401).json({ message: 'Invalid password' });
      }
    }

    const token = jwt.sign({ id: user.id, username: user.username }, SECRET_KEY, {
      expiresIn: '24h'
    });

    res.json({
      message: 'Login successful',
      token,
      user: {
        id: user.id,
        username: user.username
      }
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: 'Internal server error' });
  }
});

module.exports = router;
