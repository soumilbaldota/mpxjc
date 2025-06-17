const graalReader = require('./build/Release/graal_reader.node');
// /**
//  * Synchronously read project with the given filter
//  * @param {string} filter - The filter string to pass to readProject
//  * @returns {number} Number of entries found
//  */
function readProjectSync(filter) {
    if (typeof filter !== 'string') {
        throw new TypeError('Filter must be a string');
    }
    return graalReader.readProjectSync(filter);
}

/**
 * Asynchronously read project with the given filter
 * @param {string} filter - The filter string to pass to readProject
 * @param {function} callback - Callback function (err, result)
 */
function readProjectAsync(filter, callback) {
    if (typeof filter !== 'string') {
        throw new TypeError('Filter must be a string');
    }
    if (typeof callback !== 'function') {
        throw new TypeError('Callback must be a function');
    }
    return graalReader.readProjectAsync(filter, callback);
}

/**
 * Promise-based async version
 * @param {string} filter - The filter string to pass to readProject
 * @returns {Promise<number>} Promise that resolves to number of entries
 */
function readProject(filter) {
    return new Promise((resolve, reject) => {
        readProjectAsync(filter, (err, result) => {
            if (err) {
                reject(err);
            } else {
                resolve(result);
            }
        });
    });
}

module.exports = {
    readProjectSync,
    readProjectAsync,
    readProject
};