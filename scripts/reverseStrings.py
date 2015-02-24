"""
Jeremy Jao
2/24/2015

This was introduced because BitmapDescriptorFactory.fromAsset() no longer works...
Must use a hack to use the resource enumeration...

String Reversing....
"""

import os

def renameFile(filename):
    """
    renames the file....
    """
    routename = filename[:-4]
    os.rename(filename, 'bus_' + routename.lower() + '.png')

def reverseStrings():
    """
    Reversing strings here
    will add a lowercase b to the beginning of the file list
    reverses the string of the file
    """
    for filename in os.listdir("."):
        if not filename.startswith('b_') and filename.endswith('.png'):
            renameFile(filename)

def recoverFile(filename):
    os.rename(filename, filename[filename.find('_')+1:-4])

def recoverStrings():
    """
    recovers the filename....
    """
    for filename in os.listdir("."):
        if filename.startswith('bus_') and filename.endswith('.png'):
            recoverFile(filename)
def main():
    """
    Main...
    """
    reverseStrings()
    # recoverStrings()

if __name__ == "__main__":
    main()