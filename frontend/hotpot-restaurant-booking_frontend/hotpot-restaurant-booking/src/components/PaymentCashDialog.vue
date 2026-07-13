<style scoped>
.overlay {
  position: fixed;
  inset: 0;

  background: rgba(80, 60, 35, 0.35);

  display: flex;

  justify-content: center;

  align-items: center;

  z-index: 9999;

  backdrop-filter: blur(4px);
}

.dialog {
  width: 430px;

  max-width: 90vw;

  background: #fffdf8;

  border: 1px solid #eadfc9;

  border-radius: 20px;

  padding: 28px;

  box-shadow: 0 20px 50px rgba(120, 90, 50, 0.18);

  animation: showDialog 0.25s ease;
}

@keyframes showDialog {
  from {
    opacity: 0;
    transform: translateY(-15px) scale(0.96);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.dialog h2 {
  margin: 0 0 22px;

  text-align: center;

  color: #725b3c;

  font-size: 24px;

  font-weight: 700;
}

.dialog p {
  background: #fffaf3;

  border: 1px solid #eee2cf;

  border-radius: 14px;

  padding: 14px 16px;

  color: #806746;

  font-size: 15px;

  line-height: 1.5;

  margin-bottom: 14px;
}

.dialog strong {
  display: block;

  margin-top: 8px;

  color: #c08a3e;

  font-size: 24px;

  font-weight: 800;

  text-align: center;
}

.actions {
  display: flex;

  justify-content: flex-end;

  gap: 12px;

  margin-top: 26px;
}

.actions button {
  padding: 12px 22px;

  border-radius: 12px;

  border: none;

  font-size: 14px;

  font-weight: 700;

  cursor: pointer;

  transition: all 0.25s ease;
}

.cancel {
  background: #eee5d6;

  color: #725b3c;
}

.cancel:hover {
  background: #ded1bc;

  transform: translateY(-2px);
}

.ok {
  background: #d8b77a;

  color: white;

  box-shadow: 0 6px 15px rgba(216, 183, 122, 0.25);
}

.ok:hover {
  background: #caa565;

  transform: translateY(-2px);

  box-shadow: 0 10px 20px rgba(216, 183, 122, 0.35);
}

.ok:active,
.cancel:active {
  transform: scale(0.97);
}
</style>
