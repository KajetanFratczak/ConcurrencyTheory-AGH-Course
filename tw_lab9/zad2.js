const fs = require('fs');
const walkdir = require('walkdir');
const path = require('path');

const DATA_DIR = './data'; 

const countLines = (file) => {
    return new Promise((resolve, reject) => {
        let count = 0;
        fs.createReadStream(file)
            .on('data', function(chunk) {
                count += chunk.toString('utf8')
                .split(/\r\n|[\n\r\u0085\u2028\u2029]/g)
                .length-1;
            })
            .on('end', function() {
                resolve(count);
            })
            .on('error', reject);
    });
};

// --- Wersja 1: Synchroniczne przetwarzanie (jeden po drugim) ---
async function processSync() {
    console.time('Sync'); 
    let totalLines = 0;
    const files = walkdir.sync(DATA_DIR); 
    
    for (const file of files) {
        if (fs.lstatSync(file).isFile()) {
            const lines = await countLines(file); 
            console.log(file, lines);
            totalLines += lines;
        }
    }
    console.log('Total Sync:', totalLines);
    console.timeEnd('Sync'); 
}

// --- Wersja 2: Asynchroniczne przetwarzanie (równoległe) ---
function processAsync() {
    console.time('Async');
    let totalLines = 0;
    let paths = walkdir.sync(DATA_DIR);
    let pending = 0;
    
    let files = paths.filter(p => fs.lstatSync(p).isFile());
    
    if(files.length === 0) {
         console.timeEnd('Async');
         return;
    }

    files.forEach(file => {
        pending++;
        countLines(file).then(lines => {
            console.log(file, lines);
            totalLines += lines;
            pending--;
            if (pending === 0) {
                console.log('Total Async:', totalLines);
                console.timeEnd('Async');
            }
        });
    });
}

processSync();
//processAsync();