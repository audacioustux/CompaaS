const importObject = {
    imports: { imported_func: (arg) => state.count += arg },
};

const wasmCode = new Uint8Array(state.wasmModule);

const obj = WebAssembly.instantiate(wasmCode, importObject);
obj.then((result) => {
    result.instance.exports.exported_func()
}).catch((err) => {
    console.log('err', err);
})