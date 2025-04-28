<?php
session_start();
if (isset($_SESSION['user_role'])) {
    $role = $_SESSION['user_role'];
    if ($role === 'admin') {
        header('Location: admin.php');
        exit();
    } elseif ($role === 'teacher') {
        header('Location: teacher.php');
        exit();
    } elseif ($role === 'student') {
        header('Location: student.php');
        exit();
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Student Feedback Management System - Login</title>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" />
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 font-['Roboto'] min-h-screen flex flex-col items-center justify-center p-4">
    <main class="w-full max-w-md bg-white shadow-md rounded p-6">
        <h1 class="text-3xl font-bold text-center text-gray-800 mb-6">Login</h1>
        <form action="login.php" method="POST" class="space-y-6">
            <div>
                <label for="username" class="block text-gray-700 font-semibold mb-2">Username</label>
                <input type="text" id="username" name="username" required
                    class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div>
                <label for="password" class="block text-gray-700 font-semibold mb-2">Password</label>
                <input type="password" id="password" name="password" required
                    class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div class="text-center">
                <button type="submit"
                    class="bg-blue-600 text-white font-semibold px-6 py-3 rounded hover:bg-blue-700 transition-colors duration-300">
                    Login
                </button>
            </div>
        </form>
    </main>
</body>
</html>
