<?php
session_start();
if (!isset($_SESSION['user_role']) || $_SESSION['user_role'] !== 'admin') {
    header('Location: index.php');
    exit();
}

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "student_feedback_db";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Fetch all feedback forms and responses for admin management
$sql = "SELECT ff.id as form_id, ff.title, ff.description, c.class_name, u.full_name as teacher_name
        FROM feedback_forms ff
        JOIN classes c ON ff.class_id = c.id
        JOIN users u ON c.teacher_id = u.id
        ORDER BY ff.created_at DESC";
$result = $conn->query($sql);

$feedbackForms = [];
if ($result) {
    while ($row = $result->fetch_assoc()) {
        $feedbackForms[] = $row;
    }
}
$conn->close();
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Admin Dashboard - Student Feedback Management System</title>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" />
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 font-['Roboto'] min-h-screen p-6">
    <header class="mb-6">
        <h1 class="text-3xl font-bold text-gray-800">Admin Dashboard</h1>
        <p class="text-gray-600">Manage feedback forms and responses</p>
        <a href="logout.php" class="text-blue-600 hover:underline">Logout</a>
    </header>

    <main>
        <?php if (count($feedbackForms) === 0): ?>
            <p class="text-gray-700">No feedback forms published yet.</p>
        <?php else: ?>
            <div class="space-y-6">
                <?php foreach ($feedbackForms as $form): ?>
                    <div class="bg-white shadow rounded p-4">
                        <h2 class="text-xl font-semibold text-gray-800"><?= htmlspecialchars($form['title']) ?></h2>
                        <p class="text-gray-600 mb-2"><?= htmlspecialchars($form['description']) ?></p>
                        <p class="text-sm text-gray-500">Class: <?= htmlspecialchars($form['class_name']) ?> | Teacher: <?= htmlspecialchars($form['teacher_name']) ?></p>
                        <a href="view_responses.php?form_id=<?= $form['form_id'] ?>" class="text-blue-600 hover:underline mt-2 inline-block">View Responses</a>
                    </div>
                <?php endforeach; ?>
            </div>
        <?php endif; ?>
    </main>
</body>
</html>
