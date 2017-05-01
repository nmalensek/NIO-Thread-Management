PATH="/tmp/$USER/cs455/HW2-PC"
SCRIPT="cd $PATH;
while IFS='' read -r line || [[ -n "$line" ]]; do
	echo "$line"
done < "$1""
do
	for i in `cat machine_list`
	do
		'logging into ' $i	
		ssh $i "$SCRIPT"
	done
done 
