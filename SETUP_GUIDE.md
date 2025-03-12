# Creating a bot

1. [**Create a Discord Bot**](https://discord.com/developers/applications?new_application=true)

2. In the "Bot" tab of your app, enable all `Privileged Gateway Intents`

   ![image](https://github.com/user-attachments/assets/bf485414-6804-4c50-bebe-0b6b2f2e112c)

3. In the "Bot" tab of your app, click "Reset Token" and save your bot's token

   ![image](https://github.com/user-attachments/assets/36a3f230-1cbd-4965-97f8-21dd3491a7fe)

# Adding a bot to the server

1. Go to the "Installation" tab. In the "Guild Install" section, select `bot` from the drop-down list and specify the following permissions:

    - View Channels
    - Send Messages
    - Send Messages in Threads
    - Embed Link
    - Attach Files
    - Manage Messages
    - Read Message History

   ![image](https://github.com/user-attachments/assets/627aaff6-bd5b-449b-a612-29d49734ef10)

2. Follow the link from the "Install Link" block

    ![install link](https://github.com/user-attachments/assets/1f0aef14-29e9-4aed-a4c8-64eb08e1fb2a)

3. Authorize the bot on the server by following the instructions on Discord
4. Enable `Developer Mode` in Discord settings under the "Advanced" tab
5. Create or select an existing channel in which the bot will send messages from MineCraft
    - Make sure that the required bot permissions are not overridden in this channel.
    - Copy the channel ID by right-clicking on it and selecting "Copy Channel ID"

# Setting up the mod configuration

1. Run the server with Discord Chat Mod installed to generate `discord_chat_mod-common.toml`-config file.
2. Shut down the server and open `config/discord_chat_mod-common.toml`
3. Specify all the necessary parameters in the configuration file and save it.
4. Start the server
