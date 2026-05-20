async function login() {
    const type = document.getElementById('type').value;
    const identifier = document.getElementById('identifier').value;
    const password = document.getElementById('password').value;
    const msg = document.getElementById('msg');

    const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ type, identifier, password })
    });

    if (!res.ok) {
        msg.textContent = 'Ошибка входа';
        return;
    }

    const data = await res.json();
    localStorage.setItem('auth', JSON.stringify(data));

    if (data.role === 'GUEST') {
        window.location.href = '/app.html';
    } else {
        window.location.href = '/admin.html';
    }
}

async function addStaff() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const role = document.getElementById('role').value;
    const msg = document.getElementById('msg');

    // Проверка заполнения полей
    if (!username || !password || !role) {
        msg.textContent = 'Заполните все поля';
        return;
    }

    const requestBody = {
        username: username,
        password: password,
        role: role
    };

    console.log('Sending:', requestBody); // Для отладки

    try {
        const res = await fetch('/api/admin/staff', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });

        const text = await res.text();
        msg.textContent = text;

        if (res.ok) {
            document.getElementById('username').value = '';
            document.getElementById('password').value = '';
            document.getElementById('role').value = '';
        }
    } catch (error) {
        msg.textContent = 'Ошибка: ' + error.message;
    }
}

function getAuth() {
    return JSON.parse(localStorage.getItem('auth') || 'null');
}

async function createTicket() {
    const auth = getAuth();
    const categoryId = Number(document.getElementById('categoryId').value);
    const description = document.getElementById('description').value;

    const res = await fetch('/api/tickets', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Guest-Id': auth.guestId
        },
        body: JSON.stringify({ categoryId, description })
    });

    alert(await res.text());
    loadTickets();
}

async function loadTickets() {
    const auth = getAuth();
    const res = await fetch('/api/guest/tickets', {
        headers: { 'X-Guest-Id': auth.guestId }
    });

    const data = await res.json();
    document.getElementById('tickets').innerHTML =
        data.map(t => `<div class="ticket">
      <b>#${t.id}</b> ${t.categoryName} - ${t.status}<br>
      ${t.description}<br>
      ${t.createdAt}
    </div>`).join('');
}

async function loadAllTickets() {
    const res = await fetch('/api/admin/tickets');
    const data = await res.json();
    document.getElementById('tickets').innerHTML =
        data.map(t => `<div class="ticket">
      <b>#${t.id}</b> ${t.guestName} / ${t.roomNumber}<br>
      ${t.categoryName} - ${t.status}<br>
      ${t.description}<br>
      ${t.createdAt}
    </div>`).join('');
}