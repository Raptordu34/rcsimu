// Taille fixe de l'image
const IMAGE_WIDTH = 640;
const IMAGE_HEIGHT = 480;

// Fonction pour initialiser un canvas avec un WebSocket
function setupCanvas(canvasId, wsUrl) {
    const canvas = document.getElementById(canvasId);
    const ctx = canvas.getContext("2d");

    function resizeCanvas() {
        const rect = canvas.getBoundingClientRect();
        canvas.width = rect.width;   // largeur réelle en pixels
        canvas.height = rect.height; // hauteur réelle en pixels
    }
    window.addEventListener('resize', resizeCanvas);
    resizeCanvas();

    const ws = new WebSocket(wsUrl);
    ws.binaryType = "blob";

    const imageQueue = [];
    let drawing = false;

    function drawNextImage() {
        if (drawing || imageQueue.length === 0) return;

        drawing = true;
        const blob = imageQueue.shift();
        const img = new Image();

        img.onload = () => {
            ctx.clearRect(0, 0, canvas.width, canvas.height);

            // Centrer l'image
            const x = (canvas.width - IMAGE_WIDTH) / 2;
            const y = (canvas.height - IMAGE_HEIGHT) / 2;
            ctx.drawImage(img, x, y, IMAGE_WIDTH, IMAGE_HEIGHT);

            URL.revokeObjectURL(img.src);
            drawing = false;

            // Dessiner la prochaine image si disponible
            drawNextImage();
        };

        img.onerror = () => {
            console.error(`Erreur de chargement de l'image sur ${canvasId}`);
            drawing = false;
            drawNextImage(); // Passer à l'image suivante
        };

        img.src = URL.createObjectURL(blob);
    }

    ws.onopen = () => console.log(`Connecté au WebSocket ${canvasId}!`);

    ws.onmessage = (event) => {
        // Ajouter l'image à la file d'attente
        imageQueue.push(event.data);
        drawNextImage();
    };

    ws.onerror = (err) => console.error(`WebSocket error (${canvasId}):`, err);
    ws.onclose = () => console.log(`WebSocket ${canvasId} fermé.`);
}
