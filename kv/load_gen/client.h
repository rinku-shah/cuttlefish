#include "ue.h"
#include "utils.h"

// Thread function for each UE
void* multithreading_func(void*);
void get(Network&,UserEquipment&,int);
void put(Network&,UserEquipment&,int,int);
void getG(Network&,UserEquipment&,int);
void putG(Network&,UserEquipment&,int,int);
inline bool fileExists (const std::string&);
void setMix(int);
