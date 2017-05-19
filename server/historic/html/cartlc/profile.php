<?php

// find users system timezone using a javascript call, fed back to the server using 'GET'
if (!isset($_GET['offset'])) {
?>
<script type="text/javascript">
var d = new Date();
var ofs = d.getTimezoneOffset();
window.location.href = "profile.php?offset=" + ofs
</script>

<?php
}else{
// get offset variable (difference, in minutes, between UTC & the users TZ)
$offset = $_GET['offset'];
$offset = (int)$offset;  // convert string to int
$offset = $offset * -1;  // flip sign - logically represent offset from UTC
}

// make sure the user is logged in
include('session.php');

// database connect file
require '/var/www/includes/database.php';

//echo "Note: Call times are localized using your browser system time if available (detected timezone = UTC ".($offset/60)." hours).";

$email = $_SESSION['login_user'];

// query database to find users mobile number- needed for call records lookup
/*$stmt = $con->prepare("SELECT mobilenum FROM active WHERE email=?");
$stmt->bind_param('s', $email);
$stmt->execute();
$stmt->bind_result($mobilenum);
$stmt->fetch();
$stmt->close();*/
?>
<!DOCTYPE html>
<html>
<head>
<title>Car-TLC Records Page</title>
<link href="style.css" rel="stylesheet" type="text/css">
</head>
<body>
<div id="profile">
<b id="welcome">Welcome : <i><?php echo $email; ?></i></b>
<b id="logout"><a href="logout.php">Log Out</a></b>
</div>
<br>
<?php

$result = mysqli_query($con,"SELECT * FROM registration WHERE email='$email' ORDER BY id DESC LIMIT 1");
?>

<!--
<!DOCTYPE html>
<html>
<head>
<title>Car-TLC Records Page</title>
<link href="style.css" rel="stylesheet" type="text/css">
</head>
<body>
<div id="profile">
<b id="welcome">Welcome : <i><?php echo $email; ?></i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</b>
<b id="logout"><a href="logout.php">Log Out</a></b>
</div>
--!>

<div id="mobilenum">
<b id="mobile">Car-TLC Records for: <?php echo $email; ?></b>
</div>

<?php
echo	"<table border='1'
	<tr>
	<th>ID</th>
	<th>Email</th>
	<th>Password Hash</th>
	<th>Client Code</th>
	</tr>";

	while($row = mysqli_fetch_array($result))
	  {
	  echo "<tr>";
	  echo "<td>" . $row['id'] . "</td>";
	  echo "<td>" . $row['email'] . "</td>";
	  echo "<td>" . $row['passwd'] . "</td>";
	  echo "<td>" . $row['clientcode'] . "</td>";
	  echo "</tr>";
	  
echo	"</table>";
echo "<br>";
echo "</body>";
echo "</html>";
}

?>
