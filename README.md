![4 Mailboxes](http://challengercity.com/v4/projects/realMail/images/fourMailboxes.png)

# RealMail
RealMail adds physical mailboxes to Minecraft via dinnerbone's player skull snapshots. Basically, it adds player skulls that are textured to look like mailboxes, but they actually work.

## Download
Downloads are avaliable on [BukkitDev](http://dev.bukkit.org/bukkit-plugins/realmail/files/).

## Installation
Just drag the .jar into your server's plugins directory and restart the server. This will generate a RealMail directory in your plugins folder containing the configuration file.

## Usage
1. Craft a mailbox. The recipe is like an iron helmet in the lower-half with a chest in the center of the helmet and a piece of wool in the top-right.<br/>
![Mailbox Recipe](http://challengercity.com/v4/projects/realMail/images/mailboxRecipe.png)

2. Craft some stationery. The recipe is shapless, just a feather and a piece of paper. If the server enables it, you can also use /mail new to spawn in some free stationery.<br/>
![Stationery Recipe](http://challengercity.com/v4/projects/realMail/images/stationaryRecipe.png)

3. Write a letter on the stationery. Stationery is just a cheap Book and Quill that cannot be traded with villagers. If the first line is in the format of [Subject:my subject] , the letter's name will include the subject after being signed.
4. Attach an item to the stationery if you like. Just click the item you want to attach in your inventory and click the stationery with it. If you want to take it back off, click the stationery and right-click any empty slot.<br/>
![Attachment Demonstration](http://challengercity.com/v4/projects/realMail/images/attachmentDemo.gif)

5. Sign the book with the recipient's name.
6. Right-click any mailbox with that signed stationery (now a letter or package depending on if you attached items).
7. The recipient will then receive the letter/package in his/her mailbox either immediately or at the time of day specified in the config. Please note that only the owner of a mailbox can open his/her own mailbox unless the other player is an op, has the correct permissions to bypass the lock, or the lock_mailboxes option in the config is changed to false. Also, breaking the mailbox will not drop the letters it holds; it acts like more of an enderchest for letters.
8. The recipient can then read the letter and detach the contents if there are any.

*If the recipient's mailbox is full, the book won't be sent and the sender will be notified.

*Attachments are pretty buggy in creative, so I recommend going survival when detaching items.

## Statistics
Current usage statistics can be found on [mcstats.org](http://mcstats.org/plugin/RealMail).


## Development
RealMail is primarily developed on Windows using [Java 17.0.1](https://jdk.java.net/archive/) and [Maven 3.9.6](https://maven.apache.org/download.cgi).