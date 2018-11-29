from setuptools import setup, find_packages

setup(
    name 		= 'pyolora',
    version 	= '1.0.0c',
    description	= 'python wrapper for olorapkt',
    author		= 'gibartes',
    url			= 'https://github.com/Gibartes/O-LoRa',
    download_url= 'https://github.com/Gibartes/O-LoRa/tree/master/Device/bluetooth/python/',
    install_requires = [],
    packages    = find_packages(exclude = ['docs','*.c','*.h','makefile']),
    keyword		= 'olora',
    python_requires = '>=3',
    zip_safe    = False,
    classifiers = [
        'Programming Language :: Python :: 3',
        'Programming Language :: Python :: 3.2',
        'Programming Language :: Python :: 3.3',
        'Programming Language :: Python :: 3.4',
        'Programming Language :: Python :: 3.5',
        'Programming Language :: Python :: 3.6',
        'Programming Language :: Python :: 3.7'
    ]
)