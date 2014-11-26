#include <stdint.h>
#include <stdbool.h>
#include <stdio.h>
#include <time.h>
#include <errno.h>
#include <string.h>
 
#include <hdr_histogram.h>
#include <hdr_histogram_log.h>


int main(int argc, char** argv)
{
	int rc = 0;
	if (argc < 2)
	{
		fprintf(stderr, "Usage: %s <filename>\n", argv[0]);
		return -1;
	}

	FILE* f = fopen(argv[1], "r");

	if (!f)
	{
		fprintf(stderr, "Failed to open file(%s):%s\n", argv[1], strerror(errno));
		return -1;
	}

	printf("Processing file (%s)\n", argv[1]);

	struct hdr_log_reader reader;
	if (hdr_log_reader_init(&reader))
	{
		fprintf(stderr, "Failed to init reader\n");
		return -1;
	}

	struct hdr_histogram* h = NULL;
	struct timespec timestamp;
	struct timespec interval;

	rc = hdr_log_read_header(&reader, f);
	if(rc)
	{
		fprintf(stderr, "Failed to read header: %s\n", hdr_strerror(rc));
		return -1;
	}

	while (true)
	{
		rc = hdr_log_read(&reader, f, &h, &timestamp, &interval);

		if (0 == rc)
		{
			hdr_percentiles_print(h, stdout, 5, 1.0, CLASSIC);
		}
		else if (-1 == rc)
		{
			break;
		}
		else
		{
			fprintf(stderr, "Failed to print histogram: %s\n", hdr_strerror(rc));
			return -1;
		}		
	}

	return 0;
}