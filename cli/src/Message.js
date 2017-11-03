const chalk = require('chalk')

export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ username, userhost, userport, command, contents, time, type }) {
    this.username = username
    this.userhost = userhost
    this.userport = userport 
    this.command = command
    this.contents = contents
    this.time = time
    this.type = type
  }

  toJSON () {
    return JSON.stringify({
      username: this.username,
      command: this.command,
      contents: this.contents,
      userhost: this.userhost,
      userport: this.userport,
      time: this.time,
      type: this.type
    })
  }

  toString () {
    if (this.command === "connection alert") {
      return `${chalk.blue(this.command)}:
${chalk.white(this.time)} <${chalk.blue(this.username)}> ${chalk.blue(this.type)} ${chalk.blue(this.contents)}`
    } else if (this.command === "whisper") {
      return `${chalk.green(this.command)}:
${chalk.white(this.time)} <${chalk.green(this.username)}> ${chalk.green(this.type)} ${chalk.green(this.contents)}`
    } else if (this.command === "echo") {
      return `${chalk.yellow(this.command)}:
${chalk.white(this.time)} <${chalk.yellow(this.username)}> ${chalk.yellow(this.type)} ${chalk.yellow(this.contents)}`
    } else if (this.command === "broadcast") {
      return `${chalk.magenta(this.command)}:
${chalk.white(this.time)} <${chalk.magenta(this.username)}> ${chalk.magenta(this.type)} ${chalk.magenta(this.contents)}`
    } else if (this.command === "users") {
        return `${chalk.cyan(this.command)}:
${chalk.white(this.time)} <${chalk.cyan(this.username)}> ${chalk.cyan(this.type)} ${chalk.cyan(this.contents)}`
    } else {
    	return `${chalk.red(this.command)}:
${chalk.white(this.time)} <${chalk.red(this.username)}> ${chalk.red(this.type)} ${chalk.red(this.contents)}`
    }
  }
}
