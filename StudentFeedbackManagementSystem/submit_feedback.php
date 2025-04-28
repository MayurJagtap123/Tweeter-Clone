<?php
// Database configuration
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "student_feedback_db";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Get POST data and sanitize
$studentName = $conn->real_escape_string($_POST['studentName']);
$studentEmail = $conn->real_escape_string($_POST['studentEmail']);
$course = $conn->real_escape_string($_POST['course']);
$feedback = $conn->real_escape_string($_POST['feedback']);

// Insert feedback into database
$sql = "INSERT INTO feedback (student_name, student_email, course, feedback_text, submitted_at) VALUES ('$studentName', '$studentEmail', '$course', '$feedback', NOW())";

if ($conn->query($sql) === TRUE) {
    echo "Thank you for your feedback!";
} else {
    echo "Error: " . $sql . "<br>" . $conn->error;
}

$conn->close();
?>
