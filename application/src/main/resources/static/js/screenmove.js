const showLoginButton = document.querySelector('#loginButton');
const showRegisterButton = document.querySelector('#registerButton');
const logout = document.querySelector('#logout');
const statusModal = document.getElementById('statusModal');
const closeBtn = document.getElementById('closeModal');
const statusButton = document.getElementById('statusButton');
const matching = document.getElementById('matching');
const error1 = document.querySelector('#error-msg1');
const error2 = document.querySelector('#error-msg2');



if(showLoginButton){
showLoginButton.addEventListener('click',(e) =>{
  e.preventDefault();
  loginForm.style.display = 'block';
  registerForm.style.display = 'none';
   if(error1 || error2){
    error1.style.display = 'none';
    error2.style.display = 'none';
  }
})};

if(showRegisterButton){
showRegisterButton.addEventListener('click',function(e){
  e.preventDefault();
  loginForm.style.display = 'none';
  registerForm.style.display = 'block';
   if(error1 || error2){
    error1.style.display = 'none';
    error2.style.display = 'none';
  }
   
})};

if(logout){
  logout.addEventListener('click', function(e){
    e.preventDefault();
    const logoutConfirm = window.confirm("ログアウトしますか？");
    if(logoutConfirm){
      window.location.href = "/logout"; 
    }
  });
}

if(statusButton && statusModal){
  statusButton.addEventListener('click', function(){
    statusModal.style.display = "block";
  });
}

if(closeBtn && statusModal){
    closeBtn.addEventListener('click', function(){
        statusModal.style.display = "none";
    });
}

window.addEventListener('click', function(event) {
    if (statusModal && event.target == statusModal) {
        statusModal.style.display = "none";
    }
});