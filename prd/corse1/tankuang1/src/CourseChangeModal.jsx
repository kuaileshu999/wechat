import React, { useState, useEffect } from 'react';
import { QRCodeCanvas } from 'qrcode.react';
import { Loader2, CheckCircle2 } from 'lucide-react';

const CourseChangeModal = ({ isOpen, onClose, onFinish }) => {
  const [timeLeft, setTimeLeft] = useState(60);
  const [isFinished, setIsFinished] = useState(false);

  useEffect(() => {
    if (!isOpen) return;

    if (timeLeft > 0) {
      const timer = setInterval(() => {
        setTimeLeft((prev) => prev - 1);
      }, 1000);
      return () => clearInterval(timer);
    } else {
      setIsFinished(true);
      onFinish();
    }
  }, [isOpen, timeLeft, onFinish]);

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        {!isFinished ? (
          <div className="processing-state">
            <Loader2 className="spinner" size={48} />
            <h2>正在为您更换课程...</h2>
            <p>请稍等，大约需要 1 分钟</p>
            <div className="timer-display">
              倒计时: <span>{timeLeft}</span> 秒
            </div>
            <div className="progress-bar-container">
              <div 
                className="progress-bar" 
                style={{ width: `${((60 - timeLeft) / 60) * 100}%` }}
              ></div>
            </div>
          </div>
        ) : (
          <div className="finished-state">
            <CheckCircle2 className="success-icon" size={48} />
            <h2>课程更换成功！</h2>
            <p>扫描下方二维码添加辅导老师</p>
            <div className="qr-container">
              <QRCodeCanvas value="https://u.wechat.com/EXAMPLE_QR_CODE" size={200} />
            </div>
            <button className="close-btn" onClick={onClose}>
              我知道了
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default CourseChangeModal;
