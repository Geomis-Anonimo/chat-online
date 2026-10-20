const REMETENTE = 'CLIENTE';

let atendimento = null;
let token = localStorage.getItem('token');

const stompClient = new StompJs.Client({
    brokerURL: 'ws://' + location.host + '/ws'
});

stompClient.onConnect = async () => {
    stompClient.subscribe('/topic/atendimentos/' + atendimento.id, (message) => {
        mostrarMensagem(JSON.parse(message.body), REMETENTE);
    });
    stompClient.subscribe('/topic/atendimentos/' + atendimento.id + '/status', (message) => {
        atualizarStatus(JSON.parse(message.body));
    });

    await carregarMensagens(atendimento.id, REMETENTE, token);
};

document.getElementById('formulario').addEventListener('submit', async (event) => {
    event.preventDefault();

    const resposta = await fetch('/api/atendimentos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            nome: document.getElementById('nome').value,
            email: document.getElementById('email').value,
            celular: document.getElementById('celular').value,
            assunto: document.getElementById('assunto').value
        })
    });

    if (!resposta.ok) {
        alert('Verifique os dados informados.');
        return;
    }

    const novo = await resposta.json();
    token = novo.token;
    localStorage.setItem('atendimentoId', novo.atendimento.id);
    localStorage.setItem('token', token);
    abrirChat(novo.atendimento);
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
        destination: '/app/atendimentos/' + atendimento.id + '/mensagens',
        body: JSON.stringify({ conteudo: campo.value })
    });
    campo.value = '';
});

document.getElementById('novo').addEventListener('click', () => {
    localStorage.removeItem('atendimentoId');
    localStorage.removeItem('token');
    location.reload();
});

function abrirChat(dados) {
    atendimento = dados;
    document.getElementById('formulario').hidden = true;
    document.getElementById('chat').hidden = false;
    document.getElementById('titulo').textContent = dados.assunto;
    atualizarStatus(dados);

    limparMensagens();
    stompClient.connectHeaders = { Authorization: 'Bearer ' + token };
    stompClient.activate();
}

function atualizarStatus(dados) {
    let texto = 'Atendimento encerrado';
    if (dados.status === 'AGUARDANDO') {
        texto = 'Aguardando um atendente ficar disponível...';
    } else if (dados.status === 'EM_ANDAMENTO') {
        texto = 'Você está sendo atendido por ' + dados.atendenteNome;
    }
    document.getElementById('status').textContent = texto;

    const encerrado = dados.status === 'ENCERRADO';
    document.getElementById('form-mensagem').hidden = encerrado;
    document.getElementById('novo').hidden = !encerrado;
}

async function iniciar() {
    const id = localStorage.getItem('atendimentoId');
    if (!id || !token) {
        return;
    }

    const resposta = await fetch('/api/atendimentos/' + id, {
        headers: { 'Authorization': 'Bearer ' + token }
    });
    if (resposta.ok) {
        abrirChat(await resposta.json());
    } else if ([401, 403, 404].includes(resposta.status)) {
        localStorage.removeItem('atendimentoId');
        localStorage.removeItem('token');
    }
}

iniciar();
