all:	client

CLIENT_P = client.cpp client.h utils.cpp utils.h network.cpp network.h ue.cpp ue.h
CLIENT_R = g++ client.cpp client.h utils.cpp utils.h network.cpp network.h ue.cpp ue.h -std=c++11 -o client -lpthread -lcrypto

client:	$(CLIENT_P)
	$(CLIENT_R)
clean:
	rm -f client *~
