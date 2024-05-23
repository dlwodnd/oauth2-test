document.getElementById('kakao-login').addEventListener('click', function() {
    window.location.href = '/oauth2/authorization/kakao'; // Change this URL to your application's Google OAuth2 authorization URL
});

document.getElementById('naver-login').addEventListener('click', function() {
    window.location.href = '/oauth2/authorization/naver'; // Change this URL to your application's Facebook OAuth2 authorization URL
});