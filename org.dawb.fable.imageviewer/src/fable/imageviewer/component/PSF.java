package fable.imageviewer.component;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.embl.cca.utils.imageviewer.PointWithValueIIF;

import fable.imageviewer.component.ImageComponent;

/**
 * The <code>PSF</code> class contains parameters for applying
 * a PSF effect to an image.
 * <p>
 *
 * @author  Gabor Naray
 * @version 1.00 07/12/2011
 * @since   20111207
 */
public class PSF {
	protected int radius;
	protected int kernel[][] = null;
	protected int kernelCenter;

	public PSF( int radius ) {
		this.radius = radius;
		calculateGaussKernel();
	}

//	protected void calculateOrbKernel() {
//		final double r2 = Math.pow( radius, 2);
//		final int kernelLength = radius * 2 + 1 - 2; // Two edges are full 0
//		kernelCenter = ( kernelLength - 1 ) / 2;
//		kernel = new int[ kernelLength ][ kernelLength ];
//		for( int j = 0; j < kernelLength; j++ ) {
//			double oneMinusj2 = 1 - Math.pow( j - kernelCenter, 2 ) / r2; 
//			for( int i = 0; i < kernelLength; i++ ) {
//				kernel[ j ][ i ]
//						= (int)( Math.sqrt( oneMinusj2 - Math.pow( i - kernelCenter, 2 ) / r2 ) * 100 );
//			}
//		}
//	}
//
//	protected void calculateGaussLikeKernel() {
//		final int kernelLength = 9;
//		kernelCenter = ( kernelLength - 1 ) / 2;
//		kernel = new int[][] {
//				{ 7, 10, 15, 18, 20, 18, 15, 10,  7 },
//				{ 10, 16, 25, 35, 40, 35, 25, 16, 10 },
//				{ 15, 25, 45, 70, 80, 70, 45, 25, 15 },
//				{ 18, 35, 70, 85, 95, 85, 70, 35, 18 },
//				{ 20, 40, 80, 95, 99, 95, 80, 40, 20 },
//				{ 18, 35, 70, 85, 95, 85, 70, 35, 18 },
//				{ 15, 25, 45, 70, 80, 70, 45, 25, 15 },
//				{ 10, 16, 25, 35, 40, 35, 25, 16, 10 },
//				{ 7, 10, 15, 18, 20, 18, 15, 10,  7 } };
//	}

	protected void calculateGaussKernel() {
		final double r2 = Math.pow( radius, 2);
		final int kernelLength = radius * 2 + 1;
		kernelCenter = ( kernelLength - 1 ) / 2;
		kernel = new int[ kernelLength ][ kernelLength ];
		final double c = 2.2; //standard deviation (sigma)
		final double b = 0; //mean (mu)
		final double a = 100; //In equation: 1 / ( c * Math.sqrt( 2*Math.PI ) ), but we want constant
		final double c2m2 = 2 * Math.pow( c, 2 );
		for( int j = 0; j < kernelLength; j++ ) {
			final double j2 = Math.pow( j - kernelCenter, 2 ); 
			for( int i = 0; i < kernelLength; i++ ) {
				kernel[ j ][ i ] = (int)( a * Math.pow( Math.E,
						-Math.pow( Math.sqrt( Math.pow( i - kernelCenter, 2 ) + j2 ) - b, 2 )
						/ c2m2 ) );
			}
		}
	}

	public float[] applyPSF(ImageComponent imageComponent, Rectangle imageRect) {
//		long t0 = System.nanoTime(), t1 = t0;
		float[] imageValues = null;
		if (!imageComponent.isImageDiffOn()) {
			imageValues = imageComponent.getImageModel().getData(imageRect);
		} else {
			imageValues = imageComponent.getImageDiffModel().getData(imageRect);
		}
		/* Since Point2DWithValue are ordered by value ascending, applying the PSF
		 * in this order can be done in the gotten imageValues, because the smaller
		 * valued point does not disturb the higher valued point. Thus we do not
		 * have to clone the imageValues array, which saves time.
		 */
//		float[] newImageValues = new float[ imageValues.length ];
//		System.arraycopy( imageValues, 0, newImageValues, 0, imageValues.length );

		do {
			PointWithValueIIF[] PSFPoints = imageComponent.getStatistics().getPSFPoints();
			if( !imageComponent.isPSFOn() || PSFPoints.length == 0 )
				break;
			final int kernelMaxValue = kernel[ kernelCenter ][ kernelCenter ];
//			t1 = System.nanoTime();
//			System.out.println( "DEBUG: applyPSF.begin.dt [msec]= " + ( t1 - t0 ) / 1000000 );
//			long t11 = System.nanoTime();
//			System.out.println( "DEBUG: SYSTEMOUTPRINTLN.dt [msec]= " + ( t11 - t1 ) / 1000000 );
			for( int k = 0; k < PSFPoints.length; k++ ) {
				int x = PSFPoints[ k ].x; 
				int y = PSFPoints[ k ].y;
				int middle = y * imageRect.width + x;
				float valueMiddle = imageValues[ middle ] / kernelMaxValue;
				int yMin = Math.max( y - kernelCenter, 0);
				int yMax = Math.min( y + kernelCenter, imageRect.height - 1 );
				int xMin = Math.max( x - kernelCenter, 0);
				int xMax = Math.min( x + kernelCenter, imageRect.width - 1 );
				int topLeftOffset = yMin * imageRect.width + xMin;
				int jMin = yMin - y + kernelCenter;
				int jMax = yMax - y + kernelCenter;
				int iMin = xMin - x + kernelCenter;
				int iMax = xMax - x + kernelCenter;
				int iXY = topLeftOffset;
				int dXY = imageRect.width - ( iMax + 1 - iMin );
				float valueIJ;
				for( int j = jMin; j <= jMax; iXY += dXY, j++ ) {
					for( int i = iMin; i <= iMax; iXY++, i++ ) {
						//Ignoring 0 kernelvalue and protecting the gaps
						if( kernel[j][i] == 0 || imageValues[ iXY ] < 0 )
							continue;
						valueIJ = kernel[j][i] * valueMiddle;
						if( imageValues[ iXY ] < valueIJ )
							imageValues[ iXY ] = valueIJ;
					}
				}
			}
		} while( false );
//		long t2 = System.nanoTime();
//		System.out.println( "DEBUG: applyPSF.multiplying.dt [msec]= " + ( t2 - t1 ) / 1000000 );
		return imageValues;
	}

}
