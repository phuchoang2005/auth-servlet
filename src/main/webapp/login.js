// login.js

function showMessage(msg) {
  const result = document.getElementById("result");

  // luôn dùng textContent (không dùng innerHTML)
  result.textContent = String(msg);
}

document.getElementById("loginForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;

  // payload gửi server
  const payload = {
    username: username,
    password: password,
  };

  try {
    const response = await fetch("/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      body: JSON.stringify(payload),
    });

    const json = await response.json();

    if (response.ok) {
      const user = json?.data?.username;

      showMessage(`Đăng nhập thành công. Xin chào ${user}`);
    } else {
      const errorMsg = json?.message || "Đăng nhập thất bại";

      showMessage(`Lỗi: ${errorMsg}`);
    }
  } catch (error) {
    showMessage("Không thể kết nối tới máy chủ.");
    console.error(error);
  }
});
