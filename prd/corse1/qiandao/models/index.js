const { Sequelize, DataTypes } = require('sequelize');
const path = require('path');

const sequelize = new Sequelize({
  dialect: 'sqlite',
  storage: path.join(__dirname, '../database.sqlite'),
  logging: false
});

const User = sequelize.define('User', {
  username: {
    type: DataTypes.STRING,
    allowNull: false,
    unique: true
  },
  password: {
    type: DataTypes.STRING,
    allowNull: false
  }
});

const CheckIn = sequelize.define('CheckIn', {
  userId: {
    type: DataTypes.INTEGER,
    allowNull: false
  },
  checkInDate: {
    type: DataTypes.DATEONLY,
    allowNull: false
  }
});

// Relationships
User.hasMany(CheckIn, { foreignKey: 'userId' });
CheckIn.belongsTo(User, { foreignKey: 'userId' });

module.exports = {
  sequelize,
  User,
  CheckIn
};
