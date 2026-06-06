import { ping } from "../src/db.js";

try {
  const row = await ping();
  console.log("MySQL 连接成功");
  console.log("版本:", row.version);
} catch (err) {
  console.error("MySQL 连接失败:", err.message);
  process.exit(1);
}
