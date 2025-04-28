<?php
session_start();
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "student_feedback_db";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $user = $conn->real_escape_string($_POST['username']);
    $pass = $_POST['password'];

    $sql = "SELECT id, username, password, role FROM users WHERE username = '$user'";
    $result = $conn->query($sql);

    if ($result && $result->num_rows == 1) {
        $row = $result->fetch_assoc();
        if (password_verify($pass, $row['password'])) {
            $_SESSION['user_id'] = $row['id'];
            $_SESSION['username'] = $row['username'];
            $_SESSION['user_role'] = $row['role'];

            if ($row['role'] === 'admin') {
                header("Location: admin.php");
            } elseif ($row['role'] === 'teacher') {
                header("Location: teacher.php");
            } elseif ($row['role'] === 'student') {
                header("Location: student.php");
            }
            exit();
        } else {
            $error = "Invalid password.";
        }
    } else {
        $error = "User not found.";
    }
}
$conn->close();
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Login Error</title>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet" />
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 font-['Roboto'] min-h-screen flex flex-col items-center justify-center p-4">
    <main class="w-full max-w-md bg-white shadow-md rounded p-6 text-center">
        <h1 class="text-2xl font-bold text-red-600 mb-4">Login Error</h1>
        <p class="mb-6 text-gray-700"><?= isset($error) ? htmlspecialchars($error) : 'Unknown error.' ?></p>
        <a href="index.php" class="text-blue-600 hover:underline">Back to Login</a>
    </main>
</body>
</html>
