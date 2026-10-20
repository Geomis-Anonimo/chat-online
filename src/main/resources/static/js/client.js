const SENDER = 'CLIENT';

let atendimento = null;
let token = localStorage.getItem('token');

const stompClient = new StompJs.Client({
    brokerURL: 'ws://' + location.host + '/ws'
});

stompClient.onConnect = async () => {
    stompClient.subscribe('/topic/support-conversations/' + atendimento.id, (message) => {
        showMessage(JSON.parse(message.body), SENDER);
    });
    stompClient.subscribe('/topic/support-conversations/' + atendimento.id + '/status', (message) => {
        updateStatus(JSON.parse(message.body));
    });

    await loadMessages(atendimento.id, SENDER, token);
};

document.getElementById('form').addEventListener('submit', async (event) => {
    event.preventDefault();

    const response = await fetch('/api/support-conversations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            name: document.getElementById('name').value,
            email: document.getElementById('email').value,
            phone: document.getElementById('phone').value,
            title: document.getElementById('title').value
        })
    });

    if (!response.ok) {
        alert('Verifique os dados informados.');
        return;
    }

    const novo = await response.json();
    token = novo.token;
    localStorage.setItem('atendimentoId', novo.atendimento.id);
    localStorage.setItem('token', token);
    openChat(novo.atendimento);
});

document.getElementById('form-message').addEventListener('submit', (event) => {
    event.preventDefault();

    const campo = document.getElementById('content');
    if (!campo.value.trim()) {
        return;
    }
    if (!stompClient.connected) {
        alert('Sem conexão. Aguarde a reconexão e tente de novo.');
        return;
    }

    stompClient.publish({
        destination: '/app/support-conversations/' + atendimento.id + '/messages',
        body: JSON.stringify({ content: campo.value })
    });
    campo.value = '';
});

document.getElementById('novo').addEventListener('click', () => {
    localStorage.removeItem('atendimentoId');
    localStorage.removeItem('token');
    location.reload();
});

function openChat(dados) {
    atendimento = dados;

    document.getElementById('form').hidden = true;
    document.getElementById('chat').hidden = false;
    document.getElementById('chat-title').textContent = dados.title;

    updateStatus(dados);
    cleanMessages();

    stompClient.connectHeaders = {
        Authorization: 'Bearer ' + token
    };

    if (!stompClient.active) {
        stompClient.activate();
    }
}

function updateStatus(dados) {
    let texto = 'Atendimento encerrado';
    if (dados.status === 'WAITING') {
        texto = 'Aguardando um atendente ficar disponível...';
    } else if (dados.status === 'IN_PROGRESS') {
        texto = 'Você está sendo atendido por ' + dados.adminName;
    }
    document.getElementById('status').textContent = texto;

    const closed = dados.status === 'CLOSED';
    document.getElementById('form-message').hidden = closed;
    document.getElementById('novo').hidden = !closed;
}

async function iniciar() {
    const id = localStorage.getItem('atendimentoId');
    if (!id || !token) {
        return;
    }

    const response = await fetch('/api/support-conversations/' + id, {
        headers: { 'Authorization': 'Bearer ' + token }
    });
    if (response.ok) {
        openChat(await response.json());
    } else if ([401, 403, 404].includes(response.status)) {
        localStorage.removeItem('atendimentoId');
        localStorage.removeItem('token');
    }
}

iniciar();
