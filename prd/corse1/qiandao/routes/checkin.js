const express = require('express');
const router = express.Router();
const { CheckIn } = require('../models');
const authMiddleware = require('../middleware/auth');

// Apply auth middleware to all routes in this router
router.use(authMiddleware);

// Sign in
router.post('/', async (req, res) => {
  const userId = req.userId;
  const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD

  try {
    const existingCheckIn = await CheckIn.findOne({
      where: {
        userId,
        checkInDate: today
      }
    });

    if (existingCheckIn) {
      return res.status(400).json({ message: 'Already checked in today' });
    }

    const checkIn = await CheckIn.create({
      userId,
      checkInDate: today
    });

    res.json({
      message: 'Check-in successful',
      checkIn
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: 'Internal server error' });
  }
});

// Get check-in history
router.get('/history', async (req, res) => {
  const userId = req.userId;

  try {
    const history = await CheckIn.findAll({
      where: { userId },
      order: [['checkInDate', 'DESC']]
    });

    res.json({
      message: 'History fetched successfully',
      history
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: 'Internal server error' });
  }
});

// Check if checked in today
router.get('/today', async (req, res) => {
  const userId = req.userId;
  const today = new Date().toISOString().split('T')[0];

  try {
    const existingCheckIn = await CheckIn.findOne({
      where: {
        userId,
        checkInDate: today
      }
    });

    res.json({
      checkedIn: !!existingCheckIn
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: 'Internal server error' });
  }
});

module.exports = router;
