import socket
from time import sleep
from time import localtime
from threading import Thread
import MySQLdb as mdb
import random

def serve(client,address):
    global db;
    print("Got the connection from "+str(address));
    message=client.recv(1024).decode(encoding='utf-8');
    while(message!=''): #loop until the connection is terminated
        instruction=message.split(); #split the instruction
        command=instruction[0]; #first word is the command
        if(command=='auth'):#authenticate username and password
            #instruction for authentication is in the format of "auth userid password"
            userid=instruction[1];
            password=instruction[2];
            #check whether correct credentials or not
            #if correct insert it into online users dictionary and send true
            #else send false
            if(check(userid,password)):
                print("authentication successful: "+userid);
                #now we need to check the database for the department users and the messages
                client.send("true".encode(encoding='utf-8')); #send true as authentication is successful
                users=retriveAllUsers(userid); #retrive all users in the department
                msgs=retriveAllMsgs(userid); #retrive all the messages of the user
                client.send(str(users).encode(encoding='utf-8')); #send all the users in the department
                client.send(str(msgs).encode(encoding='utf-8')); #send all the messages to the client
                online[userid]=client; #update the online dictionary
            else:
                print("authentication failed: "+userid);
                client.send("false".encode(encoding="utf-8")); #send false as authentication is failed
        elif(command=='send'):#send message from user1 to user2
            #instruction for sending message will be in the format of "send sender receiver text"
            sender=instruction[1];
            receiver=instrunction[2];
            text=instruction[3];
            status='';
            d=localtime();#current time
            yyyy=d.tm_year;
            mm=d.tm_mon;
            dd=d.tm_mday;
            hh=d.tm_hour;
            mn=d.tm_min;
            sec=d.tm_sec;
            date=("%0.4d:%0.2d%0.2d")%(yyyy,mm,d);
            #arrange the current date as needed to insert in the database
            time=("%0.2d:%0.2d%0.2d")%(hh,mn,sec);
            #arrage the current time as needed to insert in the database
            msgid=random.randrange(0,65535); #get random id
            while(msgid in ids): #check if id already generated
                msgid=random.randrange(0,65535); #generate a new random id
            ids.add(msgid); #insert it into ids set
            if(receiver in online.keys()): #check whether the receiver is online or not
                #online so send messages to him
                status=2; #mark message as delivered
                online[receiver].send(sender+"&&"+text+"&&"+date+"&&"+time+"&&"+status);
            else:
                #offline so just store it in database
                status=1; #mark message as sent
                #mark message as sent
            #any way we need to store the messages in the database
            query="insert into messages(msgid,sender,receiver,text,date,time,status)\
values('"+msgid+"','"+sender+"','"+receiver+"','"+text+"','"+date+"','"+time+"',"+status+")";
            c=db.cursor();
            c.execute(query);
            db.commit();
            c.close();
        else:
            print("invalid command");
        message=client.recv(1024).decode(encoding='utf-8');
    print("connection terminated from "+str(address)); #acknowledgement of connection termination
    del online[userid]; #delete the user from online users
    client.close(); #close the client connection

def check(userid,password):
    global db;
    query="select * from Users where userid='"+userid+"' and password='"+password+"'";
    cursor=db.cursor();
    result=cursor.execute(query);
    if(result==1):#number of rows is 1;
        return True;
    else:
        return False;

def retriveAllMsgs(userid): #function used to for retriving all the messages of a user
    msgs=dict(); #dictionary with key as userid and value as a list [message,date,time,type,status]
    cursor=db.cursor(); #create a database cursor
    statusdict={0:'sent',1:'delivered',2:'seen'};
    #sent messages
    query="select receiver,text,date,time,status from Messages where sender='"+userid+"' order by date";
    #sent messages
    cursor.execute(query); #execute the query
    for row in cursor.fetchall(): #fetch all the sent messages
        try:
            msgs[row[0]].append([row[1],row[2],row[3],'sent',statusdict[row[4]]]); #if list already exists
        except:
            msgs[row[0]]=[[row[1],row[2],row[3],'sent',statusdict[row[4]]]]; #if list does not exist
    #received messages
    query="select sender,text,date,time,status from Messages where receiver='"+userid+"' order by date";
    #received messages
    cursor.execute(query);
    #till now list is already sorted
    for row in cursor.fetchall(): #fetch all the received messages
        try:
            #insertion sort (inserting an element into a sorted list);
            pos=len(msgs[row[0]]);
            while(pos>0 and row[3]<msgs[row[0]][pos-1][2]):
                pos=pos-1;
            msgs[row[0]].insert(pos,[row[1],row[2],row[3],'received',statusdict[row[4]]]);
            #if list already exists
        except:
            msgs[row[0]]=[[row[1],row[2],row[3],'received',statusdict[row[4]]]]; #if list does not exist
    cursor.close(); #close the cursor to save  data leakage
    print("messages of the user are: "+str(msgs));
    return msgs; #return all the messages

def retriveAllUsers(userid): #function used for retriving all the users in the same department
    global db; #global variable db
    cursor=db.cursor(); #create a cursor for the database to get department of the user
    query="select dept from Users where userid='"+userid+"'";
    cursor.execute(query);
    dept=cursor.fetchall()[0][0];
    cursor.close();
    print("User "+userid+" department is "+dept);
    users=dict(); #dictionary with key -> userid  and value -> password
    cursor=db.cursor(); #create a cursor to get the results of database queries
    query="select userid,name from Users where dept='"+dept+"' and userid<>'"+userid+"'";
    # query to get all the users in the department
    cursor.execute(query); #execute the query
    for row in cursor.fetchall(): #fetch all the rows
        users[row[0]]=row[1]; #map userid -> name
    cursor.close(); #close the cursor so that there will be no data leakage
    print("Users in the department are: "+str(users));
    return users; #return users in the same department

s=socket.socket(); #initialize socket
s_host="localhost";  #socket host name
s_port=1213; #socket port
s.bind((s_host,s_port)); #assigning the host and port to socket server
s.listen(100); #maxium number of users that can be served at a time
dbhost="localhost"; #database host name
dbport=3306; #database port number
usr="vishnureddy"; #database username
psw="12137"; #database user password
dbname="idm"; #database name
db=mdb.Connect(host=dbhost,port=dbport,user=usr,passwd=psw,db=dbname); #connect to the database
online=dict(); #which contails all the online users in it
ids=set(); #message ids set
while(True): #repeat the process until any user interruption occurs
    try:
        client_obj,client_address=s.accept(); #when user is connected then get client object and address
        thread=Thread(target=serve,args=([client_obj,client_address])); #create a parallel threads
        thread.start(); #run the thread
    except:
        client.close(); #if there is any problem at the client side then close the client connection
s.close(); #close the socket server when user interruption occurs
