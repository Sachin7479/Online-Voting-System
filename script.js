document.addEventListener("DOMContentLoaded", function() {
    var readDocs = document.querySelector(".read-instructions");
    readDocs.style.display = "block";
}); // Function to submit the vote
function submitVote() {
    // Get the selected radio button
    const selectedParty = document.querySelector('input[name="vote"]:checked');

    // Check if a party is selected
    if (!selectedParty) {
        alert('Please select a party before submitting your vote!');
        return;
    }

    // Get the party value and label
    const partyValue = selectedParty.value;
    const partyLabel = selectedParty.parentElement.querySelector('h4').textContent;

    // Hide the voting section
    document.getElementById('votingSection').style.display = 'none';

    // Show the thank you section
    document.getElementById('thankYouSection').style.display = 'block';

    // Display the selected party name
    document.getElementById('selectedParty').textContent = partyLabel;

    // Store the vote in localStorage (simulating backend storage)
    const voteData = {
        party: partyLabel,
        value: partyValue,
        timestamp: new Date().toLocaleString(),
        voterId: 'VOTER' + Math.floor(Math.random() * 100000)
    };

    localStorage.setItem('lastVote', JSON.stringify(voteData));

    // Log the vote data (in real application, this would be sent to backend)
    console.log('Vote submitted:', voteData);

    // Scroll to top to show thank you message
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Add event listeners to party cards for better UX
document.addEventListener('DOMContentLoaded', function() {
    // Get all party cards
    const partyCards = document.querySelectorAll('.party-card');

    // Add click event to each party card
    partyCards.forEach(card => {
        card.addEventListener('click', function() {
            // Find the radio button inside this card
            const radio = this.querySelector('input[type="radio"]');
            if (radio) {
                radio.checked = true;

                // Remove active class from all cards
                partyCards.forEach(c => c.classList.remove('active'));

                // Add active class to clicked card
                this.classList.add('active');
            }
        });
    });

    // Check if user has already voted (checking localStorage)
    const lastVote = localStorage.getItem('lastVote');
    if (lastVote) {
        console.log('Previous vote found:', JSON.parse(lastVote));


        // Logout button functionality
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', function() {
                // Clear any stored user data
                localStorage.clear();

                // Redirect to login page
                window.location.href = 'login.html';
            });
        }
    }
});

// Add CSS class for active party card (dynamically)
const style = document.createElement('style');
style.textContent = `
    .party-card.active {
        border-color: #667eea !important;
        background: linear-gradient(145deg, #e8ebff, #ffffff) !important;
        transform: scale(1.02);
        box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4) !important;
    }
`;
document.head.appendChild(style);