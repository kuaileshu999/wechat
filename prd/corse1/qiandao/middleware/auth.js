const jwt = require('jsonwebtoken');

const SECRET_KEY = 'your-secret-key'; // In a real app, use environment variables

module.exports = (req, res, next) => {
  const token = req.headers['authorization'];

  if (!token) {
    return res.status(401).json({ message: 'No token provided' });
  }

  // Handle "Bearer <token>" format
  const bearerToken = token.startsWith('Bearer ') ? token.slice(7) : token;

  jwt.verify(bearerToken, SECRET_KEY, (err, decoded) => {
    if (err) {
      return res.status(401).json({ message: 'Failed to authenticate token' });
    }
    req.userId = decoded.id;
    next();
  });
};
