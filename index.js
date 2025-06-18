const mpxjNode = require('./build/Release/mpxj_node.node');
// /**
//  * Synchronously read project with the given filter
//  * @param {string} filter - The filter string to pass to readProject
//  * @returns {number} Number of entries found
//  */
function readProjectSync(filter) {
    if (typeof filter !== 'string') {
        throw new TypeError('Filter must be a string');
    }
    const result = mpxjNode.readProjectSync(filter);
    // Parse the Buffer as JSON
    if (Buffer.isBuffer(result)) {
        try {
            return JSON.parse(result.toString());
        } catch (err) {
            throw new Error(`Failed to parse result as JSON: ${err.message}`);
        }
    }
    return result;
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
    return mpxjNode.readProjectAsync(filter, (err, result) => {
        if (err) {
            callback(err, null);
            return;
        }
        
        // Parse the Buffer as JSON
        if (Buffer.isBuffer(result)) {
            try {
                const parsedResult = JSON.parse(result.toString());
                callback(null, parsedResult);
            } catch (parseErr) {
                callback(new Error(`Failed to parse result as JSON: ${parseErr.message}`), null);
            }
        } else {
            callback(null, result);
        }
    });
}

/**
 * Promise-based async version
 * @param {string} filter - The filter string to pass to readProject
 * @returns {Promise<object>} Promise that resolves to the parsed JSON result
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