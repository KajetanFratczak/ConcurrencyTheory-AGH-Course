const async = require('async');

function printAsync(s, cb) {
  var delay = Math.floor((Math.random() * 1000) + 500);
  setTimeout(function () {
    console.log(s);
    if (cb) cb();
  }, delay);
}

function task1(cb) {
    printAsync("1", () => {
        cb(null); 
    });
}

function task2(cb) {
    printAsync("2", () => {
        cb(null);
    });
}

function task3(cb) {
    printAsync("3", () => {
        cb(null);
    });
}

function loop(m) {
    if (m <= 0) {
        console.log('Koniec pętli loop');
        return;
    }

    async.waterfall([
        task1,
        task2,
        task3
    ], function (err, result) {
        if (err) {
            console.error(err);
        } else {
            console.log('--- sekwencja zakończona ---\n');
            loop(m - 1);
        }
    });
}

loop(4);