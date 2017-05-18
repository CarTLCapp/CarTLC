<?php
session_start(); // Starting Session

$error=''; // Variable To Store Error Message

if (isset($_POST['submit'])) {
if (empty($_POST['email']) || empty($_POST['password'])) {
$error = "Username or Password is invalid";
}
else
{
// Define $username and $password
$email=$_POST['email'];
$password=$_POST['password'];

// Establishing Connection to Database
include '/var/www/includes/database.php';

// required to sanitize inputs if NOT using prepared statement
//$username = stripslashes($username);
//$password = stripslashes($password);
//$username = mysql_real_escape_string($username);
//$password = mysql_real_escape_string($password);

// SQL query to fetch information of registerd users and finds user match.
$stmt = $con->prepare("SELECT passwd FROM registration WHERE email=?");
$stmt->bind_param('s', $email);
$stmt->bind_result($password_hash);
$stmt->execute();
$stmt->fetch();

// validate password against the hash
if (password_verify($password, $password_hash)) {
$_SESSION['login_user']=$email; // Initializing Session
header("location: profile.php"); // Redirecting To Other Page
} else {
$error = "Username or Password is invalid";
}
$stmt->close(); // Closing Connection
}
}
?>
