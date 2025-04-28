<?php
session_start();
if (!isset($_SESSION['user_role']) || $_SESSION['user_role'] !== 'teacher') {
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

$teacher_id = $_SESSION['user_id'];

// Handle new feedback form submission
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['class_id'], $_POST['title'], $_POST['description'])) {
    $class_id = intval($_POST['class_id']);
    $title = $conn->real_escape_string($_POST['title']);
    $description = $conn->real_escape_string($_POST['description']);

    $sql = "INSERT INTO feedback_forms (class_id, title, description) VALUES ($class_id, '$title', '$description')";
    $conn->query($sql);
}

// Fetch classes for this teacher
$sql = "SELECT id, class_name FROM classes WHERE teacher_id = $teacher_id";
$result = $conn->query($sql);

$classes = [];
if ($result) {
    while ($row = $result->fetch_assoc()) {
        $classes[] = $row;
    }
}

// Fetch feedback forms published by this teacher
$sql = "SELECT ff.id, ff.title, ff.description, c.class_name
        FROM feedback_forms ff
        JOIN classes c ON ff.class_id = c.id
        WHERE c.teacher_id = $teacher_id
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
    <title>Teacher Dashboard - Student Feedback Management System</title>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" />
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 font-['Roboto'] min-h-screen p-6">
    <header class="mb-6 flex justify-between items-center">
        <div>
            <h1 class="text-3xl font-bold text-gray-800">Teacher Dashboard</h1>
            <p class="text-gray-600">Publish feedback forms for your classes</p>
        </div>
        <a href="logout.php" class="text-blue-600 hover:underline">Logout</a>
    </header>

    <section class="mb-8 bg-white shadow rounded p-4 max-w-3xl">
        <h2 class="text-xl font-semibold mb-4">Publish New Feedback Form</h2>
        <form method="POST" class="space-y-4">
            <div>
                <label for="class_id" class="block text-gray-700 font-semibold mb-2">Select Class</label>
                <select id="class_id" name="class_id" required
                    class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="" disabled selected>Select a class</option>
                    <?php foreach ($classes as $class): ?>
                        <option value="<?= $class['id'] ?>"><?= htmlspecialchars($class['class_name']) ?></option>
                    <?php endforeach; ?>
                </select>
            </div>
            <div>
                <label for="title" class="block text-gray-700 font-semibold mb-2">Feedback Form Title</label>
                <input type="text" id="title" name="title" required
                    class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div>
                <label for="description" class="block text-gray-700 font-semibold mb-2">Description</label>
                <textarea id="description" name="description" rows="4" required
                    class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
            </div>
            <div class="text-center">
                <button type="submit"
                    class="bg-green-600 text-white font-semibold px-6 py-3 rounded hover:bg-green-700 transition-colors duration-300">
                    Publish Feedback Form
                </button>
            </div>
        </form>
    </section>

    <section class="bg-white shadow rounded p-4 max-w-3xl">
        <h2 class="text-xl font-semibold mb-4">Your Published Feedback Forms</h2>
        <?php if (count($feedbackForms) === 0): ?>
            <p class="text-gray-700">You have not published any feedback forms yet.</p>
        <?php else: ?>
            <ul class="space-y-4">
                <?php foreach ($feedbackForms as $form): ?>
                    <li class="border border-gray-300 rounded p-3">
                        <h3 class="font-semibold text-lg text-gray-800"><?= htmlspecialchars($form['title']) ?></h3>
                        <p class="text-gray-600 mb-1"><?= htmlspecialchars($form['description']) ?></p>
                        <p class="text-sm text-gray-500">Class: <?= htmlspecialchars($form['class_name']) ?></p>
                    </li>
                <?php endforeach; ?>
            </ul>
        <?php endif; ?>
    </section>
</body>
</html>
