async function loadMessages(atendimentoId, sender, token) {
    const response = await fetch('/api/support-conversations/' + atendimentoId + '/messages', {
        headers: { 'Authorization': 'Bearer ' + token }
    });
    if (response.ok) {
        (await response.json()).forEach(message => showMessage(message, sender));
    }
    return response;
}

function cleanMessages() {
    document.getElementById('messages').innerHTML = '';
}

function showMessage(message, sender) {
    const lista = document.getElementById('messages');
    if (lista.querySelector('[data-id="' + message.id + '"]')) {
        return;
    }

    const div = document.createElement('div');
    div.dataset.id = message.id;
    div.className = 'message ' + (message.sender === sender ? 'minha' : 'outra');
    div.textContent = message.content;

    const hour = document.createElement('small');
    hour.textContent = new Date(message.sentDate).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    div.appendChild(hour);

    const proxima = [...lista.children].find(item => Number(item.dataset.id) > message.id);
    lista.insertBefore(div, proxima || null);
    lista.scrollTop = lista.scrollHeight;
}
