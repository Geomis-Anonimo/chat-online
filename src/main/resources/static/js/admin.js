const SENDER = 'ADMIN';

let admin = null;
let support_conversation = {};
let selected = null;
let inscricao = null;

const stompClient = new StompJs.Client({
    brokerURL: 'ws://' + location.host + '/ws'
});

stompClient.onConnect = async () => {
    stompClient.subscribe('/user/queue/support_conversations', (message) => {
        adicionarAtendimento(JSON.parse(message.body));
    });

    const response = await fetch('/api/admins/me/support_conversation', {
        headers: { 'Authorization': 'Bearer ' + admin.token }
    });
    if (sessaoValida(response)) {
        (await response.json()).forEach(adicionarAtendimento);
    }

    if (selected) {
        abrirConversa(selected.id);
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

    const dados = await response.json();
    localStorage.setItem('admin', JSON.stringify(dados));
    openPanel(dados);
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
        destination: '/app/support_conversation/' + selected.id + '/messages',
        body: JSON.stringify({ content: campo.value })
    });
    campo.value = '';
});

document.getElementById('close').addEventListener('click', async () => {
    if (!confirm('Deseja encerrar este atendimento?')) {
        return;
    }

    const response = await fetch('/api/support_conversations/' + selected.id + '/finish', {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + admin.token }
    });
    if (!sessaoValida(response)) {
        alert('Não foi possível encerrar o atendimento.');
        return;
    }

    if (stompClient.connected) {
        inscricao.unsubscribe();
    }
    inscricao = null;
    delete support_conversation[selected.id];
    selected = null;
    document.getElementById('chat').hidden = true;
    renderizarLista();
});

document.getElementById('onLogout').addEventListener('click', () => onLogout());

function onLogout() {
    localStorage.removeItem('admin');
    stompClient.deactivate();
    location.reload();
}

function sessaoValida(response) {
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

function adicionarAtendimento(dados) {
    support_conversation[dados.id] = dados;
    renderizarLista();
}

function renderizarLista() {
    const lista = document.getElementById('support_conversation');
    lista.innerHTML = '';

    const todos = Object.values(support_conversation);
    if (todos.length === 0) {
        lista.innerHTML = '<li>Nenhum atendimento no momento</li>';
        return;
    }

    todos.forEach(dados => {
        const item = document.createElement('li');
        item.textContent = dados.clientName + ' - ' + dados.title;
        item.className = 'item' + (selected && selected.id === dados.id ? ' ativo' : '');
        item.addEventListener('click', () => select(dados.id));
        lista.appendChild(item);
    });
}

function select(id) {
    if (inscricao && stompClient.connected) {
        inscricao.unsubscribe();
    }
    inscricao = null;

    selected = support_conversation[id];
    renderizarLista();
    document.getElementById('chat').hidden = false;
    document.getElementById('client-name').textContent = selected.clientName;
    document.getElementById('client-data').textContent =
        selected.title + ' | ' + selected.clientEmail + ' | ' + selected.clientPhone;

    cleanMessages();
    if (stompClient.connected) {
        abrirConversa(id);
    }
}

async function abrirConversa(id) {
    inscricao = stompClient.subscribe('/topic/support_conversations/' + id, (message) => {
        showMessage(JSON.parse(message.body), SENDER);
    });
    sessaoValida(await loadMessages(id, SENDER, admin.token));
}

const salvo = localStorage.getItem('admin');
if (salvo) {
    openPanel(JSON.parse(salvo));
}
