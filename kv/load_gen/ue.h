#include "network.h"

class UserEquipment{
	public:
		
		// Constructor
		UserEquipment(int);

		/* Functions  */
		void get(Network&, int);
		void put(Network&, int, int);

		void getG(Network&, int);
		void putG(Network&, int, int);
			
		// Destructor
		~UserEquipment();		
};
