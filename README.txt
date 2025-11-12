Smart Workload Balancer (AWT) - Fixed (No external libs)
=======================================================

This build avoids external JSON libraries by using Java object serialization.
Data file: data/db.ser (created automatically on first run)

Run:
1. cd src
2. Compile:
   javac $(find . -name "*.java") -d out
3. Run:
   java -cp out com.swb.ui.LoginFrame

Default users:
 Supervisor: admin / admin
 Workers: alice / alice123, bob / bob123, charlie / charlie123
