const SENDER = 'ADMIN';

let admin = null;
let support_conversation = {};
let selected = null;
let inscrito = null;

const stompClient = new StompJs.Client({
    brokerURL: 'ws://' + location.host + '/ws'
});

stompClient.onConnect = async () => {
    stompClient.subscribe('/user/queue/support-conversations', (message) => {
        addSupportConversation(JSON.parse(message.body));
    });

    const response = await fetch('/api/admins/me/support_conversation', {
        headers: { 'Authorization': 'Bearer ' + admin.token }
    });
    if (validSesssion(response)) {
        (await response.json()).forEach(addSupportConversation);
    }

    if (selected) {
        openTalk(selected.id);
    }
};

stompClient.onStompError = () => onLogout();

document.getElementById('login').addEventListener('submit', async (event) => {
    event.preventDefault();

    const response = await fetch('/api/admins/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            email: document.getElementById('email').value,
            password: document.getElementById('password').value
        })
    });

    if (!response.ok) {
        alert('E-mail ou senha inválidos.');
        return;
    }

    const data = await response.json();
    localStorage.setItem('admin', JSON.stringify(data));
    openPanel(data);
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
        destination: '/app/support-conversations/' + selected.id + '/messages',
        body: JSON.stringify({ content: campo.value })
    });
    campo.value = '';
});

document.getElementById('close').addEventListener('click', async () => {
    if (!confirm('Deseja encerrar este atendimento?')) {
        return;
    }

    const response = await fetch('/api/support-conversations/' + selected.id + '/finish', {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + admin.token }
    });
    if (!validSesssion(response)) {
        alert('Não foi possível encerrar o atendimento.');
        return;
    }

    if (stompClient.connected) {
        inscrito.unsubscribe();
    }
    inscrito = null;
    delete support_conversation[selected.id];
    selected = null;
    document.getElementById('chat').hidden = true;
    handleList();
});

document.getElementById('onLogout').addEventListener('click', () => onLogout());

function onLogout() {
    localStorage.removeItem('admin');
    stompClient.deactivate();
    location.reload();
}

function validSesssion(response) {
    if (response.status === 401) {
        onLogout();
    }
    return response.ok;
}

function openPanel(dados) {
    admin = dados;
    document.getElementById('login').hidden = true;
    document.getElementById('panel').hidden = false;
    document.getElementById('name-admin').textContent = dados.name;

    stompClient.connectHeaders = { Authorization: 'Bearer ' + dados.token };
    stompClient.activate();
}

function addSupportConversation(dados) {
    support_conversation[dados.id] = dados;
    handleList();
}

function handleList() {
    const list = document.getElementById('support_conversation');
    list.innerHTML = '';

    const all = Object.values(support_conversation);
    if (all.length === 0) {
        list.innerHTML = '<li>Nenhum atendimento no momento</li>';
        return;
    }

    all.forEach(data => {
        const item = document.createElement('li');
        item.textContent = data.clientName + ' - ' + data.title;
        item.className = 'item' + (selected && selected.id === data.id ? ' ativo' : '');
        item.addEventListener('click', () => select(data.id));
        list.appendChild(item);
    });
}

function select(id) {
    if (inscrito && stompClient.connected) {
        inscrito.unsubscribe();
    }
    inscrito = null;

    selected = support_conversation[id];
    handleList();
    document.getElementById('chat').hidden = false;
    document.getElementById('client-name').textContent = selected.clientName;
    document.getElementById('client-data').textContent =
        selected.title + ' | ' + selected.clientEmail + ' | ' + selected.clientPhone;

    cleanMessages();
    if (stompClient.connected) {
        openTalk(id);
    }
}

async function openTalk(id) {
    inscrito = stompClient.subscribe('/topic/support-conversations/' + id, (message) => {
        showMessage(JSON.parse(message.body), SENDER);
    });
    validSesssion(await loadMessages(id, SENDER, admin.token));
}

const salvo = localStorage.getItem('admin');
if (salvo) {
    openPanel(JSON.parse(salvo));
}
