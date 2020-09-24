# P2P_File_Transfer
Transfer files using P2P architecture in a LAN network. 

# Abstract
In the case when we have a large request of files directed to the server, files that are big in size, the server may slow down or even stop functioning because of being overloaded. So in these cases it is required to distribute the load to the users. So all of the users are required to share the downloaded files with other users that don’t poses them.
When someone wants to share a file with a few other people, he or she is required to upload the file into the server. Where others can connect and download the file. Exactly in this moment, when others try to download the file from a centralized part is where the overloading happens, it also represents a single point of failure. The Peer-to-peer (P2P) architecture eliminates these problems by distributing the data in multiple computers and uses a variety of methods to make it possible to download the data from computers which are more available, to save the system from overloading.
The realization of this system in a local network is simple, because the computer are always close together and this results in better commutating lines, so we only need to worry about the part of finding the most available computers.  This part will be realized using the Server-Client architecture because of being more adequate.

![P2P Principle](https://user-images.githubusercontent.com/7647487/94088656-53ad8000-fe11-11ea-8c1d-4a922d35debe.png)

# About this app
The application comes in two parts the server part and also the client part. The clients can't directly see the data they first need to log in.

![p2p_app_login](https://user-images.githubusercontent.com/7647487/94088837-c0287f00-fe11-11ea-91ca-f4546fd7671c.png)

You can add new clients in the server part of the app.

![p2p_app_accounts](https://user-images.githubusercontent.com/7647487/94088858-d5051280-fe11-11ea-971a-f8e8f77874d7.png)

You can see all the files in the network, and you can also add, delete, download files.

![p2p_peer](https://user-images.githubusercontent.com/7647487/94089004-3c22c700-fe12-11ea-97e5-106579192a1c.png)

# Five step file transfer
The files are transfered between two clients in five simple steps,

![p2p_5_step](https://user-images.githubusercontent.com/7647487/94089093-7d1adb80-fe12-11ea-9e50-a719777b80c6.png)

