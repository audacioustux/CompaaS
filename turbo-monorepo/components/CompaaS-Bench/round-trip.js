import WebSocket from 'ws';

const ws = new WebSocket('ws://127.0.0.2:8080/@');

ws.on('open', function open() {
    console.log('connected');
    ws.send(JSON.stringify({
        "type": "Echo",
        "msg": Date.now().toString()
    }));
});

ws.on('close', function close() {
    console.log('disconnected');
});

ws.on('message', function message(data) {
    console.log(`Round-trip time: ${Date.now() - parseInt(JSON.parse(data).msg, 10)} ms`);

    setTimeout(function timeout() {
        ws.send(JSON.stringify({
            "type": "Echo",
            "msg": Date.now().toString()
        }));
    }, 500);
});