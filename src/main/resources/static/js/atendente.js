const REMETENTE = 'ATENDENTE';

let atendente = null;
let atendimentos = {};
let selecionado = null;
let inscricao = null;

const stompClient = new StompJs.Client({
    brokerURL: 'ws://' + location.host + '/ws'
});

stompClient.onConnect = async () => {
    stompClient.subscribe('/user/queue/atendimentos', (message) => {
        adicionarAtendimento(JSON.parse(message.body));
    });

    const resposta = await fetch('/api/atendentes/me/atendimentos', {
        headers: { 'Authorization': 'Bearer ' + atendente.token }
    });
    if (sessaoValida(resposta)) {
        (await resposta.json()).forEach(adicionarAtendimento);
    }

    if (selecionado) {
        abrirConversa(selecionado.id);
    }
};

stompClient.onStompError = () => sair();

document.getElementById('login').addEventListener('submit', async (event) => {
    event.preventDefault();

    const resposta = await fetch('/api/atendentes/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            email: document.getElementById('email').value,
            senha: document.getElementById('senha').value
        })
    });

    if (!resposta.ok) {
        alert('E-mail ou senha inválidos.');
        return;
    }

    const dados = await resposta.json();
    localStorage.setItem('atendente', JSON.stringify(dados));
    abrirPainel(dados);
});

document.getElementById('form-mensagem').addEventListener('submit', (event) => {
    event.preventDefault();

    const campo = document.getElementById('conteudo');
    if (!campo.value.trim()) {
        return;
    }
    if (!stompClient.connected) {
        alert('Sem conexão. Aguarde a reconexão e tente de novo.');
        return;
    }

    stompClient.publish({
        destination: '/app/atendimentos/' + selecionado.id + '/mensagens',
        body: JSON.stringify({ conteudo: campo.value })
    });
    campo.value = '';
});

document.getElementById('encerrar').addEventListener('click', async () => {
    if (!confirm('Deseja encerrar este atendimento?')) {
        return;
    }

    const resposta = await fetch('/api/atendimentos/' + selecionado.id + '/encerrar', {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + atendente.token }
    });
    if (!sessaoValida(resposta)) {
        alert('Não foi possível encerrar o atendimento.');
        return;
    }

    if (stompClient.connected) {
        inscricao.unsubscribe();
    }
    inscricao = null;
    delete atendimentos[selecionado.id];
    selecionado = null;
    document.getElementById('chat').hidden = true;
    renderizarLista();
});

document.getElementById('sair').addEventListener('click', () => sair());

function sair() {
    localStorage.removeItem('atendente');
    stompClient.deactivate();
    location.reload();
}

function sessaoValida(resposta) {
    if (resposta.status === 401) {
        sair();
    }
    return resposta.ok;
}

function abrirPainel(dados) {
    atendente = dados;
    document.getElementById('login').hidden = true;
    document.getElementById('painel').hidden = false;
    document.getElementById('nome-atendente').textContent = dados.nome;

    stompClient.connectHeaders = { Authorization: 'Bearer ' + dados.token };
    stompClient.activate();
}

function adicionarAtendimento(dados) {
    atendimentos[dados.id] = dados;
    renderizarLista();
}

function renderizarLista() {
    const lista = document.getElementById('atendimentos');
    lista.innerHTML = '';

    const todos = Object.values(atendimentos);
    if (todos.length === 0) {
        lista.innerHTML = '<li>Nenhum atendimento no momento</li>';
        return;
    }

    todos.forEach(dados => {
        const item = document.createElement('li');
        item.textContent = dados.clienteNome + ' - ' + dados.assunto;
        item.className = 'item' + (selecionado && selecionado.id === dados.id ? ' ativo' : '');
        item.addEventListener('click', () => selecionar(dados.id));
        lista.appendChild(item);
    });
}

function selecionar(id) {
    if (inscricao && stompClient.connected) {
        inscricao.unsubscribe();
    }
    inscricao = null;

    selecionado = atendimentos[id];
    renderizarLista();
    document.getElementById('chat').hidden = false;
    document.getElementById('cliente-nome').textContent = selecionado.clienteNome;
    document.getElementById('cliente-dados').textContent =
        selecionado.assunto + ' | ' + selecionado.clienteEmail + ' | ' + selecionado.clienteCelular;

    limparMensagens();
    if (stompClient.connected) {
        abrirConversa(id);
    }
}

async function abrirConversa(id) {
    inscricao = stompClient.subscribe('/topic/atendimentos/' + id, (message) => {
        mostrarMensagem(JSON.parse(message.body), REMETENTE);
    });
    sessaoValida(await carregarMensagens(id, REMETENTE, atendente.token));
}

const salvo = localStorage.getItem('atendente');
if (salvo) {
    abrirPainel(JSON.parse(salvo));
}
