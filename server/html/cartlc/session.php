<?php

// Establishing Connection with Server by passing server_name, user_id and password as a parameter
include '/var/www/includes/database.php';

session_start();// Starting Session

// Storing Session
$user_check=$_SESSION['login_user'];

// SQL Query To Fetch Complete Information Of User
$stmt = $con->prepare("SELECT email FROM registration WHERE email=?");
$stmt->bind_param('s', $user_check);
$stmt->bind_result($login_session);
$stmt->execute();
$stmt->fetch();
if(!isset($login_session)){
$stmt->close(); // Closing Connection
header('Location: index.php'); // Redirecting To Home Page
}
?>
