import { readFileSync } from 'fs';
import { join } from 'path';

const importObject = {
    imports: { imported_func: (arg) => console.log(arg) },
};

const wasmCode = readFileSync(join('./', 'test.wasm'));
const obj = WebAssembly.instantiate(wasmCode, importObject);
obj.then((result) => {
    result.instance.exports.exported_func()
});