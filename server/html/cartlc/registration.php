<?php

/*
   secure registration platform using strong blowfish
   algorithm to create password hash.  uses random length
   salt & 10 rounds. verifies resulting hash
   against the password to ensure consistency.
   author: jeff howe jeff@jhowe.net
*/

// database connect file
require '/var/www/includes/database.php';

// pull in email & password
$clientcode = $_POST['clientcode'];  // client identifier
$email = $_POST['email'];  // users login- must be the email entered when activating
$password = $_POST['password']; // users password

// basic email validation check
if (!filter_var($email, FILTER_VALIDATE_EMAIL) === false) {
  // email passes general validation
} else {
  echo("$email is not a valid email address");
  exit();
}

// check referral code is valid
$stmt = $con->prepare("SELECT COUNT(clientcode) FROM clients WHERE clientcode = ?");
$stmt->bind_param('s', $clientcode);
$stmt->bind_result($count);
$stmt->execute();
$stmt->fetch();
$stmt->close();

if ($count != 1) {
// refcode failed validation
echo "$clientcode is not a valid client code";
exit();
}

// check referral code is valid
$stmt = $con->prepare("SELECT COUNT(clientcode) FROM registration WHERE clientcode = ?");
$stmt->bind_param('s', $clientcode);
$stmt->bind_result($count);
$stmt->execute();
$stmt->fetch();
$stmt->close();

if ($count >= 1) {
// refcode failed validation
echo "A user login for ".$clientcode." already exists!";
exit();
}

// create function to ensure use of blowfish encryption to generate hash, rounds configurable
// NOTE - 'password_hash' adds random salt values to each generated hash- YES!!! And PASSWORD_BCRYPT
// algorithm truncates the user password to 72 characters.
function better_crypt($input, $rounds = 11)
{
    $crypt_options = array(
        'cost' => $rounds
    );
    return password_hash($input, PASSWORD_BCRYPT, $crypt_options);
}

// send password to hashing function and save as string var
$password_hash = better_crypt($password);

// check username to see if user email already exists
$stmt = $con->prepare("SELECT COUNT(*) FROM registration WHERE email = ?");
$stmt->bind_param('s', $email);
$stmt->bind_result($count);
$stmt->execute();
$stmt->fetch();
$stmt->close();

if ($count > 0) {
    // email already exists, exit script
    echo $email." is already being used with an existing account login!";
    exit();
} else {
    // looking good so far
    // test hash consistency with password using PHP's built in 'password_verify' function
    if (password_verify($password, $password_hash)) {
        // password is consistent with hash, store in db using prepared statement- modify query as needed
        $stmt = $con->prepare("INSERT INTO registration (email, passwd, clientcode) VALUES (?, ?, ?)");
        $stmt->bind_param('sss', $email, $password_hash, $clientcode);
        $stmt->execute();
        $stmt->close();
        // echo success result, redirect to login page- modify as needed
        echo "CALLcheck Web registration successful, redirecting to login page...";
	header("Refresh:4; url=index.php"); // Redirecting To Login Page
    } else {
        // echo password->hash problem, password will require re-hashing
        $response["result"] = 3;
        $response["msg"]    = "hash inconsistency";
        echo json_encode($response);
    }
}

exit();

?>
