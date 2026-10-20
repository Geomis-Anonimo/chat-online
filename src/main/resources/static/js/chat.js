async function carregarMensagens(atendimentoId, remetente, token) {
    const resposta = await fetch('/api/atendimentos/' + atendimentoId + '/mensagens', {
        headers: { 'Authorization': 'Bearer ' + token }
    });
    if (resposta.ok) {
        (await resposta.json()).forEach(mensagem => mostrarMensagem(mensagem, remetente));
    }
    return resposta;
}

function limparMensagens() {
    document.getElementById('mensagens').innerHTML = '';
}

function mostrarMensagem(mensagem, remetente) {
    const lista = document.getElementById('mensagens');
    if (lista.querySelector('[data-id="' + mensagem.id + '"]')) {
        return;
    }

    const div = document.createElement('div');
    div.dataset.id = mensagem.id;
    div.className = 'mensagem ' + (mensagem.remetente === remetente ? 'minha' : 'outra');
    div.textContent = mensagem.conteudo;

    const hora = document.createElement('small');
    hora.textContent = new Date(mensagem.dataEnvio).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    div.appendChild(hora);

    const proxima = [...lista.children].find(item => Number(item.dataset.id) > mensagem.id);
    lista.insertBefore(div, proxima || null);
    lista.scrollTop = lista.scrollHeight;
}
