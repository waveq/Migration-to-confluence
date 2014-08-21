# Migration from Teamwork to Confluence


Tool that downloads your files, notebooks and representation of your category tree from [Teamwork](https://www.teamwork.com/) and uploads them to [Confluence](https://confluence.atlassian.com/). 


To make it work you have to clone the repository, import it to eclipse and perform the following steps:

1. Set your values in config.properties.

2. In `ignoredFiles` and `ignoredNotebooks` specify names of files and notebooks that will not be downloaded from Teamwork.
    
    *If you have files larger than 50MB  you will be probably unable to upload these to Confluence because Confluence CLI used here is too memory hungry and your Confluence can not stand them. I recommend adding these to IgnoredFiles.*

3. If you have mp4 files in your teamwork you have to set JDK 1.8 (32 bit!) otherwise library used to convert mp4 files will not work.

    *Mp4 files are passed through converter to lower their size. If video length is greater than 18 minutes it will cut into parts.* 

Contact:
    
    rekawekszymon at gmail.com