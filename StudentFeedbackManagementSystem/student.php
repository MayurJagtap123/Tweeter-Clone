<?php
session_start();
if (!isset($_SESSION['user_role']) || $_SESSION['user_role'] !== 'student') {
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

$student_id = $_SESSION['user_id'];

// Handle feedback submission
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['feedback_form_id'], $_POST['response'])) {
    $feedback_form_id = intval($_POST['feedback_form_id']);
    $response = $conn->real_escape_string($_POST['response']);

    // Check if student already submitted feedback for this form
    $checkSql = "SELECT id FROM feedback_responses WHERE feedback_form_id = $feedback_form_id AND student_id = $student_id";
    $checkResult = $conn->query($checkSql);

    if ($checkResult && $checkResult->num_rows === 0) {
        $insertSql = "INSERT INTO feedback_responses (feedback_form_id, student_id, response) VALUES ($feedback_form_id, $student_id, '$response')";
        $conn->query($insertSql);
        $message = "Feedback submitted successfully.";
    } else {
        $message = "You have already submitted feedback for this form.";
    }
}

// Fetch available feedback forms for student's classes
// For simplicity, assuming student is enrolled in all classes (or you can extend with enrollment table)
$sql = "SELECT ff.id, ff.title, ff.description, c.class_name, u.full_name as teacher_name
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
    <title>Student Dashboard - Student Feedback Management System</title>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" />
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 font-['Roboto'] min-h-screen p-6">
    <header class="mb-6 flex justify-between items-center">
        <div>
            <h1 class="text-3xl font-bold text-gray-800">Student Dashboard</h1>
            <p class="text-gray-600">Give feedback for your classes</p>
        </div>
        <a href="logout.php" class="text-blue-600 hover:underline">Logout</a>
    </header>

    <?php if (isset($message)): ?>
        <div class="max-w-3xl mb-6 p-4 bg-green-100 text-green-700 rounded">
            <?= htmlspecialchars($message) ?>
        </div>
    <?php endif; ?>

    <section class="bg-white shadow rounded p-4 max-w-3xl">
        <h2 class="text-xl font-semibold mb-4">Available Feedback Forms</h2>
        <?php if (count($feedbackForms) === 0): ?>
            <p class="text-gray-700">No feedback forms available at the moment.</p>
        <?php else: ?>
            <ul class="space-y-6">
                <?php foreach ($feedbackForms as $form): ?>
                    <li class="border border-gray-300 rounded p-4">
                        <h3 class="font-semibold text-lg text-gray-800"><?= htmlspecialchars($form['title']) ?></h3>
                        <p class="text-gray-600 mb-2"><?= htmlspecialchars($form['description']) ?></p>
                        <p class="text-sm text-gray-500 mb-4">Class: <?= htmlspecialchars($form['class_name']) ?> | Teacher: <?= htmlspecialchars($form['teacher_name']) ?></p>
                        <form method="POST" class="space-y-2">
                            <input type="hidden" name="feedback_form_id" value="<?= $form['id'] ?>" />
                            <label for="response_<?= $form['id'] ?>" class="block text-gray-700 font-semibold mb-1">Your Feedback</label>
                            <textarea id="response_<?= $form['id'] ?>" name="response" rows="4" required
                                class="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
                            <button type="submit"
                                class="bg-blue-600 text-white font-semibold px-5 py-2 rounded hover:bg-blue-700 transition-colors duration-300">
                                Submit Feedback
                            </button>
                        </form>
                    </li>
                <?php endforeach; ?>
            </ul>
        <?php endif; ?>
    </section>
</body>
</html>
