declare module 'mpxj-node' {

  /**
   * Synchronously read project with the given file path
   * @param {string} filePath - The path to the project file
   * @returns {any} Parsed project data
   */
  export function readProjectSync(filePath: string): any;

  /**
   * Asynchronously read project with the given file path
   * @param {string} filePath - The path to the project file
   * @param {function} callback - Callback function (err, result)
   */
  export function readProjectAsync(filePath: string, callback: (err: Error | null, result: any) => void): void;

  /**
   * Promise-based async version
   * @param {string} filePath - The path to the project file
   * @returns {Promise<any>} Promise that resolves to the parsed project data
   */
  export function readProject(filePath: string): Promise<any>;
}
